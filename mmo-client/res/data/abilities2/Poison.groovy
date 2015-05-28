import core.board.ClientBoard
import core.board.ClientCell
import core.board.ClientAbility
import shared.board.Board
import shared.board.Cell
import shared.board.Ability
import shared.board.Unit
import core.graphics.scenes.BattleScene
import core.graphics.scenes.Scenes
import program.main.SceneUtil

def onCheck(Ability spell, Board board, Cell target){
    target.contentsType == Cell.CONTENTS_UNIT && target.unit.state != Unit.STATE_DEAD
}

def onCheckAOE(Ability spell, Board board, Cell from, Cell to){
    from == to
}

def onCastBegin(ClientAbility spell, Board board, ClientCell target){
	spell.caster.setFacing(target.unit);
	1.0F
}

def onCastEnd(Ability spell, ClientBoard board, Cell target){
    def from = SceneUtil.getScene(Scenes.BATTLE, BattleScene.class).getSpatialByUnit(spell.caster).node,
        to = SceneUtil.getScene(Scenes.BATTLE, BattleScene.class).getSpatialByUnit(target.unit).node;

    board.addBuff("PoisonBuff", 4, 1, target.unit)
	board.nextTurn()
	spell.putOnCoolDown()

    board.addEffect("fireball-effect", from, to)
}