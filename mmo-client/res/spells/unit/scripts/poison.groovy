import core.board.Board
import core.board.Cell
import core.board.Spell
import core.board.Unit
import core.graphics.scenes.BattleScene
import core.graphics.scenes.Scenes
import program.main.Util

def onCheck(Spell spell, Board board, Cell target){
    target.contentsType == Cell.CONTENTS_UNIT && target.unit.state != Unit.STATE_DEAD
}

def onCheckAOE(Spell spell, Board board, Cell from, Cell to){
    from == to
}

def onCastBegin(Spell spell, Board board, Cell target){
	spell.caster.setFacing(target.unit);
	1.0F
}

def onCastEnd(Spell spell, Board board, Cell target){
    def from = Util.getScene(Scenes.BATTLE, BattleScene.class).getSpatialByUnit(spell.caster).node,
        to = Util.getScene(Scenes.BATTLE, BattleScene.class).getSpatialByUnit(target.unit).node;

    board.addBuff("poisonBuff", 1, 6, target.unit)
	board.nextTurn()
	spell.putOnCoolDown()

    board.addEffect("fireball-effect", from, to)
}